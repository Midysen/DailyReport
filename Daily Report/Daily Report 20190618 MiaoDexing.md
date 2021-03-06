# Setup an F-Droid App Repo
Anyone can create their own repo, and users can control which repos their client is using, including even disabling the default f-droid.org repo. 

## Overview
If you want to maintain a simple binary repository of APKs and packages obtained elsewhere, the process is quite simple:

- Set up the server tools     <https://www.f-droid.org/en/docs/Installing_the_Server_and_Repo_Tools>
- create a directory called fdroid, then run fdroid init in that directory
- Optionally edit the config.py to your liking, details examples are in ./examples/config.py
- Within fdroid, make a directory called repo and put APK files in it.
- Run fdroid update.
- If it reports that any metadata files are missing, you can create them in the metadata directory and run it again.
- To ease creation of metadata files, run fdroid update with the -c option. It will create ’skeleton’ metadata files that are missing, and you can then just edit them and fill in the details.
- Then, if you’ve changed things, run fdroid update again.
- Running fdroid update adds an icons directory into the repo directory, and also creates the repository index files (index.xml, index.jar, etc) NOTE: To make this process secure, read Real World Setup below!
- Publish the resulting contents of the repo directory to your web server (or set serverwebroot in your config.py then use fdroid server update)
<br>

Following the above process will result in a repo directory, which you simply need to push to any HTTP (or preferably HTTPS) server to make it accessible.


## Local Demo Repo HOWTO

- This is a full HOWTO to setup your own repository wherever you want to host it. It is somewhat technical, you will use the terminal, but you don’t need to be a terminal expert to follow along. First, this HOWTO will walk through setting up a test repo that is not very secure. Then it will walk through setting up a repo for real world use, with the signing key on a separate machine from the public webserver. Before you start, you need to get the fdroidserver tools and a webserver. For the webserver, this HOWTO will use nginx since its lightweight, but any will do if you already have one running.
```
    sudo apt-get install nginx
    
```
- 更改/etc/nginx/sites-available/default配置文件，修改nginx的工作目录
```
 root /var/www/;  
```
- 创建fdroid目录
```
sudo mkdir /var/www/fdroid
sudo chown -R $USER /var/www/fdroid
```
- 服务器需要安装openssh-server
```
sudo apt-get install openssh-server
```

# Real World Setup

To improve this situation, generate the repo on a non-public machine like your laptop, keeping config.py and the keystore only on that machine (remember to make backups!). Then use fdroid server update to publish the changes to your repo on a separate server via ssh. So start a new repo from scratch on your non-public machine:
```
mkdir ~/fdroid
cd ~/fdroid
fdroid init
cp /path/to/\*.apk ~/fdroid/repo/
fdroid update --create-metadata
vim config.py # add the serverwebroot, etc: serverwebroot = 'linux@192.168.0.198:/var/www/fdroid' 
fdroid server update -v
```
至此，会将fdroid目录下的部分文件上传到192.168.0.198的/var/www/fdroid目录下，这里要保证linux对/var/www/目录有操作权限<br>
Now edit config.py to set serverwebroot, it is in the form of a standard SCP file destination. 
Then fdroid server update will do the publishing via rsync over ssh. 
So both computers will have to have ssh and rsync installed and setup. 
You can also use your own existing signing key rather than the one generated by fdroid init, just edit repo_keyalias, keystore, keystorepass, keypass, and keydname in ~/fdroid/config.py.

# How to Add a Repo to F-Droid

https://www.f-droid.org/en/tutorials/add-repo/
