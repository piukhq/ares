import json
import requests
import os

webhook = 'https://hellobink.webhook.office.com/webhookb2/bf220ac8-d509-474f-a568-148982784d19@a6e2367a-92ea-4e5a-b565-723830bcc095/IncomingWebhook/c5f9ef43f199443b974face37694378e/8ca3aa12-4603-452c-ae1e-b241aef04c87'#os.environ.get('MOBILE_TEAMS_WEBHOOK') 
url = os.environ.get('BITRISE_BUILD_URL') + '?tab=artifacts'
cvss = 6.9
score = 50

payload = """ {
    "@type": "MessageCard",
    "@context": "http://schema.org/extensions",
    "themeColor": "0076D7",
    "summary": "Static Analysis Report",
    "sections": [{
        "activityTitle": "^-^ Bunk the VulnerabilityBot ^-^",
        "activitySubtitle": "SAST Android Report",
        "markdown": true,
    }, 
    {
        "activityTitle": "Static Analysis Report",
        "activityText": "MobSF has processed a Static Analysis report of the APK.",
        "facts": [{
            "name": "Average CVSS",
            "value": "%s",
        }, {
            "name": "Security Score",
            "value": "%s",
        }],
    }, {
        "text": "NOTE: When selecting See Report, you need to navigate to <b>Apps and Artifacts</b> and open the report.pdf file.",
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
""" % (cvss, score, url)

print(payload)

headers = {'content-type': 'application/json'}
r = requests.post(webhook, data=payload, headers=headers)
print(r.json)
print(r.raw)