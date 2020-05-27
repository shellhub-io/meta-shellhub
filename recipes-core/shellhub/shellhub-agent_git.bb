SUMMARY = "ShellHub Agent"
HOMEPAGE = "https://shellhub.io"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/src/${GO_IMPORT}/LICENSE.md;md5=fa818a259cbed7ce8bc2a22d35a464fc"
DEPENDS = "libxcrypt"

SRC_URI = " \
    git://github.com/shellhub-io/shellhub;branch=master \
    file://shellhub-agent.initd \
    file://shellhub-agent.profile.d \
    file://shellhub-agent.service \
    file://shellhub-agent.start \
"

SRCREV = "8c0ee51c145f66afd6d3d8dab8f273c6596178a5"
PV = "0.3.1"

inherit go systemd update-rc.d

# Avoid dynamic linking as it causes segfault
GO_LINKSHARED = ""

SYSTEMD_SERVICE_${PN} = "${PN}.service"

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 99"

GO_IMPORT = "github.com/shellhub-io/shellhub"

GO_LDFLAGS = '-ldflags="${GO_RPATH} ${GO_LINKMODE} -X main.AgentVersion=${PV} -extldflags '${GO_EXTLDFLAGS}'"'

GOBUILDFLAGS_append = " -modcacherw"

do_compile[dirs] += "${B}/src/${GO_IMPORT}/agent"

do_install_append() {
    # We name the binary as shellhub-agent
    mv ${D}${bindir}/agent ${D}${bindir}/shellhub-agent

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
}

RDEPENDS_${PN} += "shellhub-agent-config"
RRECOMMENDS_${PN} += "ca-certificates"
RDEPENDS_${PN}-dev += "bash"
