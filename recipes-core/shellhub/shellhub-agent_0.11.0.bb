SUMMARY = "ShellHub Agent"
HOMEPAGE = "https://shellhub.io"
LICENSE = "Apache-2.0"

GO_IMPORT = "github.com/shellhub-io/shellhub"
GO_INSTALL = "github.com/shellhub-io/shellhub/agent"

LIC_FILES_CHKSUM = "file://src/${GO_IMPORT}/LICENSE.md;md5=fa818a259cbed7ce8bc2a22d35a464fc"

DEPENDS = "libxcrypt"

SRC_URI = " \
    git://github.com/shellhub-io/shellhub;protocol=https;nobranch=1;destsuffix=git/src/${GO_IMPORT} \
    file://shellhub-agent.initd \
    file://shellhub-agent.profile.d \
    file://shellhub-agent.service \
    file://shellhub-agent.start \
    file://shellhub-agent.wrapper.in \
"

# v0.11.0: latest practical ShellHub Agent target for stock Yocto kirkstone / Go 1.17
SRCREV = "0189c253e45ab63026c3c18b786db22949e4ba86"

S = "${WORKDIR}/git"

inherit go systemd update-rc.d

GO_LINKSHARED = ""

SYSTEMD_SERVICE:${PN} = "${PN}.service"

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 99"

GO_LDFLAGS = '-ldflags="${GO_RPATH} ${GO_LINKMODE} -X main.AgentVersion=v0.11.0 -extldflags '${GO_EXTLDFLAGS}'"'

GOBUILDFLAGS:append = " -modcacherw"

do_compile[dirs] += "${B}/src/${GO_IMPORT}/agent"
do_compile[network] = "1"

do_install:append() {
    install -d ${D}${libexecdir}/shellhub/bin/

    if [ -f ${D}${bindir}/agent ]; then
        mv ${D}${bindir}/agent ${D}${libexecdir}/shellhub/bin/shellhub-agent
    else
        bbfatal "shellhub agent binary not found at ${D}${bindir}/agent"
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -Dm 0644 ${WORKDIR}/shellhub-agent.service ${D}${systemd_unitdir}/system/shellhub-agent.service
        sed -i -e 's,@BINDIR@,${bindir},g' ${D}${systemd_unitdir}/system/shellhub-agent.service
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','true','false',d)}; then
        install -Dm 0755 ${WORKDIR}/shellhub-agent.initd ${D}${sysconfdir}/init.d/shellhub-agent
        install -Dm 0755 ${WORKDIR}/shellhub-agent.start ${D}${libdir}/shellhub-agent/shellhub-agent.start
        sed -e 's,@BINDIR@,${bindir},g' \
            -e 's,@LIBDIR@,${libdir},g' \
            -e 's,@LOCALSTATEDIR@,${localstatedir},g' \
            -e 's,@SYSCONFDIR@,${sysconfdir},g' \
            -i ${D}${sysconfdir}/init.d/shellhub-agent
    fi

    install -Dm 0755 ${WORKDIR}/shellhub-agent.profile.d ${D}${sysconfdir}/profile.d/shellhub-agent.sh

    install -Dm 0755 ${WORKDIR}/shellhub-agent.wrapper.in ${D}${bindir}/shellhub-agent
    sed -e 's,@LIBEXEC@,${libexecdir},g' -i ${D}${bindir}/shellhub-agent
}

RDEPENDS:${PN} += " \
    openssh-scp \
    shellhub-agent-config \
    shadow \
"

RRECOMMENDS:${PN} += "ca-certificates"
RDEPENDS:${PN}-dev += "bash"

