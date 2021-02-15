#!/bin/bash

import json
import requests
import os
import time
from requests_toolbelt.multipart.encoder import MultipartEncoder

mobsfFiles = os.environ.get('BITRISE_APK_PATH')
mobsfUser = os.environ.get('BITRISE_MOBSF_USER')
mobsfPass = os.environ.get('BITRISE_MOBSF_PASS')
mobsfApiKey = os.environ.get('BITRISE_MOBSF_API_KEY')
mobsfDest = os.environ.get('BITRISE_DEPLOY_DIR')

multipart_data = MultipartEncoder(fields={'file': (mobsfFiles, open(mobsfFiles, 'rb'), 'application/octet-stream')})

print('== MobSF 1/3 == \nUpload Process Starting')
url = 'https://mobsf.uksouth.bink.sh/api/v1/upload'
headers = {'X-Mobsf-Api-Key': mobsfApiKey, 'Content-Type': multipart_data.content_type}
r = requests.post(url, data=multipart_data, headers=headers, auth=(mobsfUser, mobsfPass))
print('Upload Process Complete - ', r.status_code)

json = r.json()
hash = json['hash']
filename = json['file_name']
scantype = json['scan_type']

url = 'https://mobsf.uksouth.bink.sh/api/v1/scan'

headers = {'X-Mobsf-Api-Key': mobsfApiKey}
files = {
    'hash': hash, 
    'file_name': mobsfFiles, 
    'scan_type': scantype, 
    're_scan': 0
}

print('== MobSF 2/3 == \nScan Starting')
time.sleep(5)
r = requests.post(url, data=files, headers=headers, auth=(mobsfUser, mobsfPass))

##"average_cvss": 6.9,
    ##"security_score": 50
json = r.json()
cvss = json.get('average_cvss')
score = json.get('security_score')

print('Scan Complete - ', r.status_code)

url = 'https://mobsf.uksouth.bink.sh/api/v1/download_pdf'

print('== MobSF 3/3 == \nReport Downloading')
time.sleep(5)
r = requests.post(url, data={'hash': hash}, headers=headers, auth=(mobsfUser, mobsfPass))

if r:
    print('Report Downloaded - ', r.status_code)
    with open(mobsfDest + '/report.pdf', 'wb') as f:
        print('Report Saved')
        f.write(r.content)
        webhook = os.environ.get('MOBILE_TEAMS_WEBHOOK') 
        url = os.environ.get('BITRISE_BUILD_URL') + '?tab=artifacts'

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
else:
    print('An error has occurred.')
    exit(1)
