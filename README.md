🌟 Super-Auto 超星学习通自动刷课平台

![GitHub stars](https://img.shields.io/github/stars/DuanInnovator/SuperAutoStudy?style=social)
![GitHub forks](https://img.shields.io/github/forks/DuanInnovator/SuperAutoStudy?style=social)
![GitHub issues](https://img.shields.io/github/issues/DuanInnovator/SuperAutoStudy)
![GitHub license](https://img.shields.io/github/license/DuanInnovator/SuperAutoStudy)

> ✨ "基于Springboot+RabbitMQ+docker+Dubbo的超星学习通自动刷课平台"

📚 文档入口
• [📖 使用文档](https://doc.xxtmooc.com) - 详细的使用指南和配置说明


👥 欢迎加入SuperAuto交流群: [1033757261](https://qm.qq.com/q/1033757261)

🎯 功能特性

✅ 稳定高效：使用学习通官方API配合时间欺骗+模拟用户观看，高效完成学习通任务  
✅ 多任务处理：使用自定义ThreadPoolExecutor实现多任务并行执行  
✅ 智能答题：支持多种题库接入，自动完成章节测验  
✅ Docker支持：一键部署，轻松运行  
✅ 分布式架构：基于Dubbo的微服务架构，支持水平扩展  

🔥 当前版本功能 (v1.0.1 | 2025-04-26)

📌 核心功能：
• 支持多任务同时运行

• 自动完成视频观看、章节测验

• 支持学习通平台全功能


📌 新增特性：
• 优化任务调度算法

• 增强稳定性检测机制

• 修复已知BUG


💡 题库配置指南

| 参数             | 描述                      | 是否必须       | 示例值                              | Token获取方式                |
|----------------|-------------------------|------------|----------------------------------|--------------------------|
| use            | 使用题库列表(默认所有免费题库) | 否          | local,icodef,buguake,wanneng     | -                        |
| wannengToken   | 万能付费题库Token(10位)    | 否          | E196FD8B49                       | [获取地址](https://lyck6.cn/pay) |
| icodefToken    | Icodef题库Token          | 否          | UafYcHViJMGzSVNh                 | 关注微信公众号"一之哥哥"获取      |
| enncyToken     | enncy题库Token           | 否          | a21ae2403b414b94b512736c30c69940 | [官网](https://tk.enncy.cn) |
| aidianYToken   | 爱点题库Token             | 否          | cvor7f3HxZ7nF2M3ljmA             | [官网](https://www.51aidian.com) |
| lemonToken     | 柠檬题库Token             | 否          | 8a3debe92e2ba83d6786e186bef2a424 | [官网](https://www.lemtk.xyz) |

```yaml
# application-dev.yml 配置示例
tiku:
  settings:
    endpoints:
      - name: "icodef"
        token: "xQtsFM16W6KpXCBt"
      - name: "wanneng"
        token: "E196FD8B49"
```


![GitHub stars](https://api.star-history.com/svg?repos=DuanInnovator/SuperAutoStudy&type=Date)
🐛 问题反馈

🙋‍♂️ 欢迎提出ISSUE！如果您遇到任何问题或有改进建议：
1. 请先查阅[文档](https://doc.xxtmooc.com)
2. 在[GitHub Issues](https://github.com/DuanInnovator/SuperAutoStudy/issues)提交问题
3. 或加入QQ群反馈：1033757261

我们重视每一位用户的反馈！🎉

---

⭐ 如果觉得项目不错，欢迎Star支持！您的支持是我们持续更新的动力~
