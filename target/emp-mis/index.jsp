<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>EMP 员工管理 MIS 系统 · 接口清单</title>
    <style>
        body { font-family: "Microsoft YaHei", Arial, sans-serif; margin: 40px; color: #333; }
        h1 { border-bottom: 2px solid #4a90d9; padding-bottom: 8px; }
        table { border-collapse: collapse; width: 100%; margin-top: 16px; }
        th, td { border: 1px solid #ccc; padding: 8px 12px; text-align: left; font-size: 14px; }
        th { background: #4a90d9; color: #fff; }
        code { background: #f4f4f4; padding: 2px 6px; border-radius: 3px; }
        .tip { background: #fff8e1; border-left: 4px solid #f0ad4e; padding: 10px 14px; margin-top: 20px; }
    </style>
</head>
<body>
<h1>EMP 员工管理 MIS 系统（SSM 全注解版）</h1>

<p>数据库：<code>ssm_emp</code> · 数据表：<code>emp</code> · 上下文路径：<code>/emp-mis</code></p>

<table>
    <tr>
        <th>功能</th>
        <th>方法</th>
        <th>路径</th>
        <th>说明</th>
    </tr>
    <tr><td>登录（建立会话）</td><td>POST</td><td><code>/auth/login</code></td><td>body: <code>{"username":"admin"}</code></td></tr>
    <tr><td>校验登录态</td><td>GET</td><td><code>/auth/check</code></td><td>返回是否登录</td></tr>
    <tr><td>退出</td><td>POST</td><td><code>/auth/logout</code></td><td>销毁会话</td></tr>
    <tr><td>新增员工</td><td>POST</td><td><code>/emps</code></td><td>body 为员工 JSON</td></tr>
    <tr><td>修改员工</td><td>PUT</td><td><code>/emps</code></td><td>body 必须含 empId</td></tr>
    <tr><td>删除员工</td><td>DELETE</td><td><code>/emps/{id}</code></td><td>路径参数</td></tr>
    <tr><td>批量删除</td><td>DELETE</td><td><code>/emps</code></td><td>body: <code>[5,6]</code></td></tr>
    <tr><td>单条查询</td><td>GET</td><td><code>/emps/{id}</code></td><td>返回员工详情</td></tr>
    <tr><td>分页多条件查询</td><td>GET</td><td><code>/emps?page=1&size=5&name=&dept=</code></td><td>支持 name/gender/dept/status/minSalary</td></tr>
    <tr><td>薪资排行</td><td>GET</td><td><code>/emps/top?limit=3</code></td><td>在职员工按薪资倒序</td></tr>
    <tr><td>事务回滚演示</td><td>POST</td><td><code>/emps/tx-demo</code></td><td>新增后抛异常，验证回滚</td></tr>
</table>

<div class="tip">
    <strong>调试提示：</strong>登录拦截器拦截除 <code>/auth/**</code> 外的所有请求，
    未登录访问将返回 <code>{"code":401,...}</code>。请先用 ApiFox 调用
    <code>POST /auth/login</code> 建立会话（保持 Cookie），再访问 <code>/emps/**</code>。
</div>
</body>
</html>
