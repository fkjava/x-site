#!/bin/bash

# 整个脚本需要使用sudo申请root权限来执行

user=$(env | grep USERNAME | cut -d "=" -f 2)
if [ "$user" == "root" ]; then
    echo "正在使用root用户执行初始化脚本"
else
    echo "必须使用root用户运行初始化脚本，管理员可以在脚本命令之前加上sudo命令"
    exit 0
fi

# 把运行脚本的目录存储到一个变量中
SCRIPT_DIR=`pwd`

# 加载函数库，用于后面定义函数
. /lib/lsb/init-functions

enable_ufw(){
    echo "正在激活防火墙，激活防火墙后，除了22和80端口外，其他端口均未开通..."
    # 开启22端口，给ssh协议使用
    ufw allow 22
    # 开启80端口，给HTTP协议使用
    ufw allow 80
    # 激活防火墙，--force表示强制执行
    ufw --force enable
    echo "防火墙激活成功"
}

upgrade_sys(){
    echo "正在更新软件包..."
    # 更新软件仓库索引
    apt -y update
    # 更新软件包
    apt -y upgrade
    # 自动删除无用软件包
    apt -y autoremove
    # 更新中文语言包
    apt -y install language-pack-zh-hans
    echo "软件包更新成功"
}

install_nginx(){
    echo "正在安装Nginx服务器，使用默认配置，启用80端口..."
    # 安装Nginx服务器，可以把nginx改为apache2
    sudo apt -y install nginx
    echo "Nginx安装成功"
}
gcc_ready=0
install_gcc(){
    echo "正在安装GCC和TCL软件包..."
    # 安装GCC环境，用于编译Redis
    apt -y install build-essential
    apt -y install tcl
    gcc_ready=1
    echo "GCC和TCL安装成功"
}

libaio_ready=0
install_libaio(){
    echo "正在安装libaio..."
    # 安装libaio，用于给MySQL使用
    apt -y install libaio-dev
    libaio_ready=1
    echo "libaio安装成功"
}

install_mysql() {
    if [ $libaio_ready == 0 ]; then
        install_libaio
    fi
    echo "正在安装MySQL..."
    cd $SCRIPT_DIR
    # 下载MySQL手动安装包，需要自己配置和初始化数据库
    wget https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.15-linux-glibc2.12-x86_64.tar.xz
    # 移动文件到/usr/local/目录下
    mv mysql-8.0.15-linux-glibc2.12-x86_64.tar.xz /usr/local/
    cd /usr/local/
    tar -Jxf mysql-8.0.15-linux-glibc2.12-x86_64.tar.xz
    ln -s mysql-8.0.15-linux-glibc2.12-x86_64 mysql
    cd mysql
    mkdir etc
    mkdir /data/mysql-8.0.15
    # 创建MySQL配置文件
    echo "[mysqld]" > etc/my.cnf
    echo "lower-case-table-names=1" >> etc/my.cnf
    echo "character-set-server=utf8mb4" >> etc/my.cnf
    # 设置MySQL数据文件存储目录
    echo "datadir=/data/mysql-8.0.15" >> etc/my.cnf

    # 增加用户
    useradd mysql
    # 改变MySQL程序的所有者
    chown -R mysql:mysql .
    chown -R mysql:mysql /data/mysql-8.0.15


    # 执行MySQL初始化脚本，并且不生成随机密码
    ./bin/mysqld  --user=mysql --initialize-insecure

    # 增加MySQL自动启动脚本
    cp  ./support-files/mysql.server  /etc/init.d/mysql
    update-rc.d  mysql  defaults
    # 启动MySQL
    service mysql start

    # 修改MySQL数据库root用户的密码为1234，后面再登录就需要使用密码了
    ./bin/mysqladmin -u root password '1234'

    # 增加MySQL相关的命令到PATH环境变量
    echo "PATH=/usr/local/mysql/bin:\$PATH" > /etc/profile.d/mysql.sh
    source /etc/profile.d/mysql.sh
    echo "MySQL安装成功"
}

install_redis(){
    if [ $gcc_ready == 0 ]; then
        install_gcc
    fi
    echo "正在安装Redis..."
    cd $SCRIPT_DIR
    # 下载Redis
    wget http://download.redis.io/releases/redis-5.0.3.tar.gz
    tar -zxf redis-5.0.3.tar.gz
    cd redis-5.0.3
    # 编译和安装Redis
    make
    make test
    make install

    # 配置一个Redis的默认服务器实例
    mkdir  /etc/redis
    cp  redis.conf  /etc/redis/6379.conf
    sed -i 's/bind 127.0.0.1/#bind 127.0.0.1/g' /etc/redis/6379.conf
    sed -i 's/protected-mode yes/protected-mode no/g' /etc/redis/6379.conf
    sed -i 's/daemonize no/daemonize yes/g' /etc/redis/6379.conf
    sed -i 's/logfile ""/logfile "redis.log"/g' /etc/redis/6379.conf
    sed -i 's/dir .\//dir \/data\/redis\/6379/g' /etc/redis/6379.conf

    mkdir -p /data/redis/6379

    # 配置Redis自动启动
    cp  utils/redis_init_script  /etc/init.d/redis_6379
    update-rc.d  redis_6379  defaults
    service  redis_6379  start
    echo "Redis安装成功"
}

install_jdk(){
    echo "正在JDK 11..."
    # 安装和配置JDK
    cd $SCRIPT_DIR
    wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz
    mv openjdk-11.0.2_linux-x64_bin.tar.gz /usr/local/
    cd /usr/local/
    tar -zxf openjdk-11.0.2_linux-x64_bin.tar.gz
    ln -s jdk-11.0.2 jdk
    echo "PATH=/usr/local/jdk/bin:\$PATH" > /etc/profile.d/jdk.sh
    source /etc/profile.d/jdk.sh
    echo "JDK 11安装成功"
}


case "$1" in
    all)
        enable_ufw
        upgrade_sys
        install_nginx
        install_gcc
        install_libaio
        install_mysql
        install_redis
        install_jdk
    ;;
    ufw)
        enable_ufw
    ;;
    nginx)
        install_nginx
    ;;
    gcc)
        install_gcc
    ;;
    libaio)
        install_libaio
    ;;
    mysql)
        install_mysql
    ;;
    redis)
        install_redis
    ;;
    jdk)
        install_jdk
    ;;
    upgrade)
        upgrade_sys
    ;;
    *)
        echo $"Usage: $0 {all|ufw|upgrade|nginx|gcc|libaio|mysql|redis|jdk}"
        echo "  all : 执行所有的初始化配置操作"
        echo "  ufw : 激活防火墙，并放通22端口和80端口"
        echo "  upgrade : 升级已经安装的软件包"
        echo "  nginx : 安装Nginx服务器"
        echo "  gcc : 安装GCC编译器和TCL环境，用于安装Redis"
        echo "  libaio : 安装libaio软件包"
        echo "  mysql : 安装libaio和MySQL，其中MySQL是在官方网站上下载的免安装版，安装以后把数据库密码自动设置为1234"
        echo "  redis : 安装Redis数据库，自动从官方网站下载Redis的源代码进行编译、安装"
        exit 1
esac








