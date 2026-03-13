# 使用Tomcat基础镜像（JDK 8）
FROM tomcat:9.0-jdk8

# 设置工作目录
WORKDIR /usr/local/tomcat

# 删除默认的webapps内容
RUN rm -rf /usr/local/tomcat/webapps/*

# 复制WAR文件到webapps目录
COPY target/simulation-oa.war /usr/local/tomcat/webapps/ROOT.war

# 暴露端口
EXPOSE 8080

# 启动Tomcat
CMD ["catalina.sh", "run"]
