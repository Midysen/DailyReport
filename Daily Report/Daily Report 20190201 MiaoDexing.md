#  git中refs/for 和refs/heads
- 简单点说，就是refs/for/mybranch需要经过code review之后才可以提交；refs/heads/mybranch不需要code review。
  -  如果需要code review，直接push
  ```
  git push origin master
  ```
  这样就会有问题，那么就会有“! [remote rejected] master -> master (prohibited by Gerrit)”的错误信息
  -  而这样push就没有问题，
  ```
  git push origin HEAD:refs/for/mybranch
  ```
  
# gerrit 不能推送 merge
```
git push origin HEAD:refs/for/master

  Counting objects: 203, done.
  Delta compression using up to 4 threads.
  Compressing objects: 100% (37/37), done.
  Writing objects: 100% (59/59), 6.84 KiB | 0 bytes/s, done.
  Total 59 (delta 26), reused 0 (delta 0)
  remote: Resolving deltas: 100% (26/26)
  remote: Processing changes: refs: 1, done
  To ssh://yuzx@xxx.com:29418/xxxrowd
  ! [remote rejected] HEAD -> refs/for/master (you are not allowed to upload merges)
  error: 无法推送一些引用到 'ssh://yuzx@xxx.xxx.com:29418/xxxrowd'
```
- 解决办法
```
git rebase
  Cannot rebase: You have unstaged changes.

git stash
# 每次 push 前
git pull --rebase
git push origin HEAD:refs/for/master

# gerrit review
git pull
git stash pop

```
因在 master 上开发，导致和远程冲突，这时 git pull 默认 merge 模式
gerrit 不允许 merge 后提交，需 rebase 方式，所以，本地 pull 以 rebase 方式进行，即：
```
git pull –rebase
```
