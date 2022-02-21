# -*- coding: utf-8 -*-
"""
Author : Lei zhang
Contact: 756689324@qq.com
Date   : 2020/8/3 9:34
Desc   : 调用腾讯新闻摘要接口
"""
from pandas._libs import json

import params
from apicomm import ApiComm
from tencentcloud.common import credential
from tencentcloud.common.profile.client_profile import ClientProfile
from tencentcloud.common.profile.http_profile import HttpProfile
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.nlp.v20190408 import nlp_client, models
class Tencent(ApiComm):
    def __init__(self):
        super().__init__()
        self.id=params.Tencent_ID
        self.key=params.Tencent_KEY
        self.length=params.Length
        self.client = self.connect()
    def connect(self):
        cred = credential.Credential(self.id, self.key)
        httpProfile = HttpProfile()
        httpProfile.endpoint = "nlp.tencentcloudapi.com"

        clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        client = nlp_client.NlpClient(cred, "ap-guangzhou", clientProfile)
        return client
    def summary(self,text):
        req = models.AutoSummarizationRequest()
        param = '{}'
        end='}'
        if self.length:
            l='{"Length":'+str(self.length)
            param=l+end
            if text:
                param=l+',"Text":"'+text+'"}'
        else:
            param = '{"Text":"' + text + '"}'
        req.from_json_string(param)
        resp = self.client.AutoSummarization(req)
        st=resp.to_json_string()
        dict=json.loads(st)
        return dict["Summary"]

if __name__=='__main__':
    api=Tencent()
    txtname = "txt3.txt"
    txtnames = 'txt3summary.txt'
    with open(txtname, "r", encoding='utf-8') as f:
        text = str(f.readlines());
        print(text)
    length=300
    summary=api.summary(text)
    print(summary)
    with open(txtnames, "a", encoding='utf-8') as f:
        f.write('\n腾讯新闻摘要结果：\n')
        f.write(summary)
