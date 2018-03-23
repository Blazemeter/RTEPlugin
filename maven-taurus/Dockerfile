FROM maven:3.5

RUN apt-get -y update
RUN apt-get -y install python default-jre-headless python-tk python-pip python-dev libxml2-dev libxslt-dev zlib1g-dev net-tools
RUN pip install bzt
RUN pip install --upgrade bzt 