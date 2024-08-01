import requests
import json

SONARQUBE_URL = 'http://localhost:9000'
API_TOKEN = 'your_api_token'
PROJECT_KEY = 'your_project_key'

def fetch_project_metrics():
    url = f"{SONARQUBE_URL}/api/measures/component"
    params = {
        'component': PROJECT_KEY,
        'metricKeys': 'bugs,vulnerabilities,code_smells,coverage'
    }
    headers = {
        'Authorization': f'Basic {API_TOKEN}'
    }
    response = requests.get(url, params=params, headers=headers)
    data = response.json()
    return data

def save_report(data):
    with open('weekly_report.json', 'w') as f:
        json.dump(data, f, indent=4)

if __name__ == "__main__":
    data = fetch_project_metrics()
    save_report(data)
