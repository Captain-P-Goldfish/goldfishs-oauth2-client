FROM amazoncorretto:21-alpine3.21

ENV TARGETDIR=/opt/oauth-test-client
ENV CONFIGDIR=${TARGETDIR}/config
ENV LOGSDIR=${TARGETDIR}/logs
ENV LIBSDIR=${TARGETDIR}/libs

WORKDIR ${TARGETDIR}

# Copy the freshly built artifact
COPY maven/ ${TARGETDIR}
RUN mkdir ${CONFIGDIR}
RUN mkdir ${LOGSDIR}

# Create user to run the executable
RUN addgroup --gid 500 oauthusergroup && \
    adduser --system --disabled-password --no-create-home --uid 500 --ingroup oauthusergroup sck && \
    chgrp -R oauthusergroup ${CONFIGDIR} && \
    chmod g=r ${CONFIGDIR} && \
    chgrp -R oauthusergroup ${LOGSDIR} && \
    chmod g+rw ${LOGSDIR}
USER sck

# Standard port
EXPOSE 9543

ENTRYPOINT java -Dloader.path=${LIBSDIR}/ -jar oauth2-test-client.jar --spring.config.additional-location=${CONFIGDIR}/application.properties
