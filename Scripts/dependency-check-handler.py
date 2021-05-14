import json
import requests
import os

location = os.environ.get('BITRISE_SOURCE_DIR')
url = os.environ.get('BITRISE_BUILD_URL')
webhook = os.environ.get('MOBILE_TEAMS_WEBHOOK')
pipename = os.environ.get('CI_PIPELINE_NAME') + " Pipeline"

print(location)
print(url)

with open(location + '/app/build/reports/dependency-check-report.json') as f:
    data = json.load(f)

deps = data['dependencies']

depsWithVulns = []
totalVulnsCount = 0
vulnsFound = ""
for dep in deps:
    vulns = dep.get('vulnerabilities')
    if vulns is not None:
        vulnList = []
        totalVulnsCount += len(vulns)
        for vuln in vulns:
            vulnList.append(vuln['name'] + " " + vuln['severity'])
        depName = dep.get('fileName')
        depsWithVulns.append('<b>' + depName + '</b> - ' + str(len(vulnList)) + ' issues' + '\n' + ',\n'.join(map(str,vulnList)))

# Dependencies Scanned Count
depsScannedCount = str(len(deps))

# Vulnerabilities Found Count
if len(depsWithVulns) > 0:
    vulnsFound = '\n'.join(map(str, depsWithVulns))
else:
    vulnsFound = '0 vulnerabilities in the application'

payload = """ {
    "@type": "MessageCard",
    "@context": "http://schema.org/extensions",
    "themeColor": "0076D7",
    "summary": "Vulnerability Report",
    "sections": [{
        "activityTitle": "^-^ Bunk the VulnerabilityBot ^-^",
        "activitySubtitle": "Android Report",
        "facts": [{
            "name": "Dependencies Scanned",
            "value": "%s",
        }, {
            "name": "Vulnerabilities Discovered",
            "value": "%s",
        }],
        "markdown": true
    }, 
    {
        "activityTitle": "%s",
        "activitySubtitle": "Result",
        "activityText": "<pre>%s</pre>",
        "text": "NOTE: When selecting See Report, you need to navigate to <b>Apps and Artifacts</b> to see the HTML report in detail.",
    }
    ],
    "potentialAction": [{
        "@type": "OpenUri",
        "name": "See Report",
        "targets": [{
            "os": "default",
            "uri": "%s"
        }]
    }]
}
""" % (depsScannedCount, str(totalVulnsCount), pipename, vulnsFound, url)

print(payload)

url = webhook
headers = {'content-type': 'application/json'}
r = requests.post(url, data=payload, headers=headers)
print(r.json)
print(r.raw)