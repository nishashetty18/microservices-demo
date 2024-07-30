/*
 * Copyright 2018, Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hipstershop;

import hipstershop.Demo.Ad;
import hipstershop.Demo.AdRequest;
import hipstershop.Demo.AdResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A simple client that requests ads from the Ads Service. */
public class AdServiceClient {

  private static final Logger logger = LogManager.getLogger(AdServiceClient.class);

  private final ManagedChannel channel;
  private final hipstershop.AdServiceGrpc.AdServiceBlockingStub blockingStub;

  /** Construct client connecting to Ad Service at {@code host:port}. */
  private AdServiceClient(String host, int port) {
    this(
        ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext() // Disable TLS for simplicity in example.
            .build());
  }

  /** Construct client for accessing Ads Service using the existing channel. */
  private AdServiceClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = hipstershop.AdServiceGrpc.newBlockingStub(channel);
  }

  /** Shutdown the channel gracefully. */
  private void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /** Get Ads from Server. */
  public void getAds(String contextKey) {
    logger.info("Requesting Ads with context: " + contextKey);
    AdRequest request = AdRequest.newBuilder().addContextKeys(contextKey).build();
    AdResponse response;

    try {
      response = blockingStub.getAds(request);
      for (Ad ad : response.getAdsList()) {
        logger.info("Ad: " + ad.getText() + " (Redirect URL: " + ad.getRedirectUrl() + ")");
      }
    } catch (StatusRuntimeException e) {
      logger.log(Level.ERROR, "RPC failed: " + e.getStatus(), e);
    }
  }

  /** Extract port from command line arguments or use default. */
  private static int getPortOrDefaultFromArgs(String[] args) {
    int portNumber = 9555;
    if (args.length > 1) {
      try {
        portNumber = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        logger.warn("Port {} is invalid, using default port {}", args[1], portNumber);
      }
    }
    return portNumber;
  }

  /** Extract string from command line arguments or use default. */
  private static String getStringOrDefaultFromArgs(String[] args, int index, @Nullable String defaultString) {
    if (index < args.length) {
      return args[index];
    }
    return defaultString;
  }

  /**
   * Ads Service Client main method. The first argument is the context key for fetching ads,
   * the second argument is the server port.
   */
  public static void main(String[] args) throws InterruptedException {
    final String contextKey = getStringOrDefaultFromArgs(args, 0, "camera");
    final String host = getStringOrDefaultFromArgs(args, 1, "localhost");
    final int serverPort = getPortOrDefaultFromArgs(args);

    logger.info("Connecting to Ad Service at {}:{}", host, serverPort);

    AdServiceClient client = new AdServiceClient(host, serverPort);
    try {
      client.getAds(contextKey);
    } finally {
      client.shutdown();
    }

    logger.info("Exiting AdServiceClient...");
  }
}
