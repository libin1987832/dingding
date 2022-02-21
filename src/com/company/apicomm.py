# -*- coding: utf-8 -*-
"""
Author : Lei zhang
Contact: 756689324@qq.com
Date   : 2020/8/3 9:23
Desc   : 接口类
"""
class ApiComm:
    def __init__(self):
        pass
    def connect(self):
        raise NotImplementedError('请实现connect方法,处理具体的功能.')
    def summary(self,text):
        raise NotImplementedError('请实现summary方法,处理具体的功能.')