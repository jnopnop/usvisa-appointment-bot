FROM amazonlinux:2 as builder

RUN yum install -y unzip && \
        curl -Lo "/tmp/chromedriver-linux64.zip" "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/120.0.6099.71/linux64/chromedriver-linux64.zip" && \
        curl -Lo "/tmp/chrome-linux64.zip" "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/120.0.6099.71/linux64/chrome-linux64.zip" && \
        unzip /tmp/chromedriver-linux64.zip -d /opt/ && \
        unzip /tmp/chrome-linux64.zip -d /opt/

FROM amazonlinux:2
RUN yum install -y java-17 atk cups-libs gtk3 libXcomposite alsa-lib \
    libXcursor libXdamage libXext libXi libXrandr libXScrnSaver \
    libXtst pango at-spi2-atk libXt xorg-x11-server-Xvfb \
    xorg-x11-xauth dbus-glib dbus-glib-devel nss mesa-libgbm

COPY --from=builder /opt/chrome-linux64 /opt/chrome
COPY --from=builder /opt/chromedriver-linux64 /opt/

WORKDIR /app

COPY target/visa-appointment-scheduler.jar /app

CMD ["java", "-jar", "visa-appointment-scheduler.jar", \
      "--chrome.chrome-driver-path=/opt/chromedriver", \
      "--chrome.chrome-path=/opt/chrome/chrome"]
