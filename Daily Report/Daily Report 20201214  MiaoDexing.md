# Dockerfile

```
FROM ubuntu:18.04                                                                                                                                                                                       
COPY ./file.sh/  /
COPY ./test.sh/  /
ENTRYPOINT ["/bin/bash"]
CMD ["/file.sh","arg1", "arg2"]


```

- FROM：定制的镜像都是基于 FROM 的镜像，这里的 nginx 就是定制需要的基础镜像。后续的操作都是基于 nginx。

-  RUN：用于执行后面跟着的命令行命令
  - 注意：Dockerfile 的指令每执行一次都会在 docker 上新建一层。所以过多无意义的层，会造成镜像膨胀过大。以 && 符号连接命令，这样执行后，只会创建 1 层镜像。
- COPY 复制指令，从上下文目录中复制文件或者目录到容器里指定路径。
- ADD 指令和 COPY 的使用格式一致（同样需求下，官方推荐使用 COPY）。功能也类似，不同之处如下：

    - ADD 的优点：在执行 <源文件> 为 tar 压缩文件的话，压缩格式为 gzip, bzip2 以及 xz 的情况下，会自动复制并解压到 <目标路径>。
    - ADD 的缺点：在不解压的前提下，无法复制 tar 压缩文件。会令镜像构建缓存失效，从而可能会令镜像构建变得比较缓慢。具体是否使用，可以根据是否需要自动解压来决定。
 - CMD
   类似于 RUN 指令，用于运行程序，但二者运行的时间点不同:
 
    -   CMD 在docker run 时运行。
    -   RUN 是在 docker build。

    - 作用：为启动的容器指定默认要运行的程序，程序运行结束，容器也就结束。CMD 指令指定的程序可被 docker run 命令行参数中指定要运行的程序所覆盖。

    - 注意：如果 Dockerfile 中如果存在多个 CMD 指令，仅最后一个生效。
    
- ENTRYPOINT

   类似于 CMD 指令，但其不会被 docker run 的命令行参数指定的指令所覆盖，而且这些命令行参数会被当作参数送给 ENTRYPOINT 指令指定的程序。

  - 但是, 如果运行 docker run 时使用了 --entrypoint 选项，此选项的参数可当作要运行的程序覆盖 ENTRYPOINT 指令指定的程序。

  - 优点：在执行 docker run 的时候可以指定 ENTRYPOINT 运行所需的参数。

  - 注意：如果 Dockerfile 中如果存在多个 ENTRYPOINT 指令，仅最后一个生效。

  - 格式：
  ```
   ENTRYPOINT ["<executeable>","<param1>","<param2>",...]

   可以搭配 CMD 命令使用：一般是变参才会使用 CMD ，这里的 CMD 等于是在给 ENTRYPOINT 传参，以下示例会提到。
   ```
- 然后在主机外壳
   ```
   docker build -t test . 
   docker run -i -t test 
   ```
