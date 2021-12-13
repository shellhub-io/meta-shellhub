SUMMARY = "ShellHub Agent"
HOMEPAGE = "https://shellhub.io"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/src/${GO_IMPORT}/LICENSE.md;md5=fa818a259cbed7ce8bc2a22d35a464fc"
DEPENDS = "libxcrypt"

SRC_URI = " \
    git://github.com/shellhub-io/shellhub;protocol=https;nobranch=1;tag=v${PV} \
    file://shellhub-agent.initd \
    file://shellhub-agent.profile.d \
    file://shellhub-agent.service \
    file://shellhub-agent.start \
    file://shellhub-agent.wrapper.in \
"

inherit go systemd update-rc.d

# Avoid dynamic linking as it causes segfault
GO_LINKSHARED = ""

SYSTEMD_SERVICE:${PN} = "${PN}.service"

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 99"

GO_IMPORT = "github.com/shellhub-io/shellhub"

GO_INSTALL = "github.com/shellhub-io/shellhub/agent"

GO_LDFLAGS = '-ldflags="${GO_RPATH} ${GO_LINKMODE} -X main.AgentVersion=v${PV} -extldflags '${GO_EXTLDFLAGS}'"'

GOBUILDFLAGS:append = " -modcacherw"

do_compile[dirs] += "${B}/src/${GO_IMPORT}/agent"

do_install:append() {
    # We name the binary as shellhub-agent
    mkdir -p ${D}${libexecdir}/shellhub/bin/
    mv ${D}${bindir}/agent ${D}${libexecdir}/shellhub/bin/shellhub-agent

    # Handle init system integration
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -Dm 0644 ${WORKDIR}/shellhub-agent.service ${D}${systemd_unitdir}/system/shellhub-agent.service
        sed -i -e 's,@BINDIR@,${bindir},g' ${D}${systemd_unitdir}/system/shellhub-agent.service
    fi
    if ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','true','false',d)}; then
        install -Dm 0755 ${WORKDIR}/shellhub-agent.initd ${D}/${sysconfdir}/init.d/shellhub-agent
        install -Dm 0755 ${WORKDIR}/shellhub-agent.start ${D}${libdir}/shellhub-agent/shellhub-agent.start
        sed -e 's,@BINDIR@,${bindir},g' \
            -e 's,@LIBDIR@,${libdir},g' \
            -e 's,@LOCALSTATEDIR@,${localstatedir},g' \
            -e 's,@SYSCONFDIR@,${sysconfdir},g' \
            -i ${D}/${sysconfdir}/init.d/shellhub-agent
    fi

    # Shell prompt handling
    install -Dm 0755 ${WORKDIR}/shellhub-agent.profile.d ${D}/${sysconfdir}/profile.d/shellhub-agent.sh

    # Script that allow to run sh files before shellhub-agent binary start
    install -Dm 0755 ${WORKDIR}/shellhub-agent.wrapper.in ${D}${bindir}/shellhub-agent
    sed -e 's,@LIBEXEC@,${libexecdir},g' -i ${D}${bindir}/shellhub-agent
}

RDEPENDS:${PN} += "\
    openssh-scp \
    shellhub-agent-config \
    shadow \
"

RRECOMMENDS:${PN} += "ca-certificates"
RDEPENDS:${PN}-dev += "bash"
