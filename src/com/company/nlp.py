from aip import AipNlp
import json
""" 你的 APPID AK SK """
APP_ID = '21665539'
API_KEY = 'atZ6OvTc99agGFuATzDAUUgc'
SECRET_KEY = 'V9SB1a2k7qnH0sHR6InlyrHtEgo754rl'

client = AipNlp(APP_ID, API_KEY, SECRET_KEY)
content = "央广网北京2月28日消息 据中国之声《新闻和报纸摘要》报道，国务院总理李克强2月27日向第五届中德创新大会致贺信。李克强在贺信中表示，\
当前新一轮科技革命和产业变革席卷全球，科技创新正深刻改变着人类的生产生活方式。中德科技创新合作开创了大国科技合作的先例，为两国务实合作装上了大功率“引擎”。\
李克强指出，中国经济发展正处在新旧动能转换和结构升级的关键时期。我们将贯彻落实新发展理念，深入实施创新驱动发展战略，促进大众创业、万众创新上水平，\
加快建设创新型国家。希望中德双方汇集众智、增进共识，深化科技创新交流合作，推动两国经济社会健康发展，为全球经济注入新动力。\
中德政府间科技合作协定签订40周年暨第五届中德创新大会27日在京举行。两国科技、企业、政府等各界300余名代表出席。"
content = "麻省理工学院的研究团队为无人机在仓库中使用RFID技术进行库存查找等工作，创造了一种聪明的新方式。\
使用RFID标签更换仓库中的条形码，将帮助提升自动化并提高库存管理的准确性。几家公司已经解决了无人机读取RFID的技术问题。\
麻省理工学院的新解决方案，名为Rfly，允许无人机阅读RFID标签，而不用捆绑巨型读卡器。无人机接收从远程RFID读取器发送的信号，\
然后转发它读取附近的标签。"
maxSummaryLen = 300
txtname = "txt3.txt"
txtnames = 'txt3summary.txt'
with open(txtname,"r",encoding='utf-8') as f:
    content = f.readlines();
    print(content)
""" 调用新闻摘要接口 """
res=client.newsSummary(content, maxSummaryLen)

print(res['summary'])
with open(txtnames,"a",encoding='utf-8') as f:
    f.write('\n百度新闻摘要结果：\n')
    f.write(res['summary'])


