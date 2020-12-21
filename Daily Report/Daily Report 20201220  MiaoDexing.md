# linux shell取文本最后一行
-  awk 'END {print}'
 
-  sed -n '$p'
 
-  sed '$!N;$!D'
 
-  awk '{b=a"\n"$0;a=$0}END{print b}'

# Linux shell去除字符串中所有空格
- echo $VAR | sed 's/ //g'

# Shell判断字符串包含关系的几种方法
- 方法一
```
strA="long string"
strB="string"
result=$(echo $strA | grep "${strB}")
if [[ "$result" != "" ]]
then
    echo "包含"
else
    echo "不包含"
fi
```
- 方法二
```
A="helloworld"
B="low"
if [[ $A == *$B* ]]
then
    echo "包含"
else
    echo "不包含"
fi
```

- 方法三

```
STRING_A=$1
STRING_B=$2
if [[ ${STRING_A/${STRING_B}//} == $STRING_A ]]
    then
        ## is not substring.
        echo N
        return 0
    else
        ## is substring.
        echo Y
        return 1
    fi
```
- 方法四 正则表达式中的通配符 *:
```
str1="abcdefgh"
str2="def"
if [[ $str1 == *$str2* ]];then
    echo "包含"
else
    echo "不包含"
fi
```

# 指定范围随机数
```


[root@sds ~]# echo $(($RANDOM%11))
 6
[root@sds ~]# echo $(($RANDOM%11))
 9
[root@sds ~]# echo $(($RANDOM%11))
 3



```
- 产生三位数的随机数
```
[root@server shell02]# echo $[$RANDOM%900+100]
713
[root@server shell02]# echo $[$RANDOM%900+100]
686
[root@server shell02]# echo $[$RANDOM%900+100]
474
[root@server shell02]# echo $[$RANDOM%900+100]
202
```
- 产生一个n~m范围内的随机数

使用$RANDOM取余m-n+1，之后加上n就可以了
```
1 -- 100 的随机数
num=$[$RANDOM%100+1]
```
