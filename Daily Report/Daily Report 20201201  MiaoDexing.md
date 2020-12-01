# git commit 生成专用PATCH及合入PATCH的方法

## 一、根据git 提交记录生成PATCH
1、git format-patch -1 commit_id 生成git专用PATCH
如：git format-patch -1 e3faf9e06b6d1ca46d79e972ebf54daf00f68d87
生成：0001-test.patch 的补丁

2、检查该PATCH信息
如：git apply --stat 0001-test.patch

3、检查该PATCH是否能在指定源代码中合入
如：git apply --check 0001-test.patch
error: zyf/FileTrans/test.txt：已经存在于工作区中

如果没有任何输出，则表示可合入。如有错误会在终端输出。

## 二、合入PATCH到指定代码库中
1、git pull更新代码

2、git apply  0001-test.patch 合入到本地

3、git status 查看PATCH的修改

4、git commit -m "提交说明"

5、git push origin master 将代码推送到服务器的主分支上

## 三、使用diff生成通用PATCH
1、使用diff commit1 commit2 > diff.patch可以生成通用PATCH，表示从commit1到commit2(含commit2)的修改生成PATCH文件
2、使用patch -p(n) < diff.patch 合入PATCH，其中 n 代表路径的层级

## 四、注意事项
推荐使用git format-patch生成git 专用PATCH，因为我们在实际使用中发现，如果使用diff生成通用PATCH，对于删除文件的操作会出现失败的情况。如果没有删除操作的情况下diff的效率及通用性会比较好。
