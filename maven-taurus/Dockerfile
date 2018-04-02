FROM maven:3.5

RUN apt-get -y update \
  && apt-get -y install python default-jre-headless python-tk python-pip python-dev libxml2-dev libxslt-dev zlib1g-dev net-tools \
  && pip install bzt \
  && apt-get -y install tightvncserver \
  && curl -fsSL -o /usr/share/execute-on-vnc.sh https://raw.githubusercontent.com/croesch/beefiles/master/src/execute-on-vnc.sh \
  && chmod +x /usr/share/execute-on-vnc.sh \
  && ln -s /usr/share/execute-on-vnc.sh /usr/bin/execute-on-vnc