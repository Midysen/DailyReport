# 第一步 启动qemu
```
sudo qemu-system-x86_64  -cdrom android-x86_7.1/out/target/product/x86_64/android_x86_64.iso  -hda ./vdisk.img -smp 1 -m 1G -enable-kvm -netdev user,net=192.168.85.11/24,dhcpstart=192.168.85.16,hostfwd=tcp::5555-:5555,id=vmnet1, -device e1000,netdev=vmnet1,id=device-net0,mac=aa:54:00:11:22:33  -vga std
```
# 第二步
- adb connect  localhost:5555
- adb shell
  -  setprop persist.fb.vmifb 0
  -  dumpsys SurfaceFlinger --switchPrimaryCompose
