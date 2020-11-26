# 第一步 启动qemu
```
sudo qemu-system-x86_64  -cdrom android-x86_7.1/out/target/product/x86_64/android_x86_64.iso  -hda ./vdisk.img -smp 1 -m 1G -enable-kvm -netdev user,net=192.168.85.11/24,dhcpstart=192.168.85.16,hostfwd=tcp::5555-:5555,id=vmnet1, -device e1000,netdev=vmnet1,id=device-net0,mac=aa:54:00:11:22:33  -vga std
```
# 第二步
- adb connect  localhost:5555
- adb shell
  -  setprop persist.fb.vmifb 0
  -  dumpsys SurfaceFlinger --switchPrimaryCompose


# jcenter下载不了时，用国内镜像下载解决
```

    buildscript{

        repositories{

            maven{ url'http://maven.aliyun.com/nexus/content/groups/public/' }

            maven{ url'http://maven.aliyun.com/nexus/content/repositories/jcenter'}

            google()

    }

        dependencies{

            classpath'com.android.tools.build:gradle:3.1.4'

            // NOTE: Do not place your application dependencies here; they belong

    // in the individual module build.gradle files

        }

    }

    allprojects{

        repositories{

            maven{ url'http://maven.aliyun.com/nexus/content/groups/public/' }

            maven{ url'http://maven.aliyun.com/nexus/content/repositories/jcenter'}

    }

    }



```
