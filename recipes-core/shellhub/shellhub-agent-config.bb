SUMMARY = "ShellHub Configuration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
INHIBIT_DEFAULT_DEPS = "1"

do_patch[noexec] = "1"
do_configure[noexec] = "1"

SHELLHUB_SERVER_ADDRESS ??= "http://ssh.shellhub.io"
SHELLHUB_PRIVATE_KEY    ??= "${sysconfdir}/shellhub-agent.key"

do_compile () {
    cat <<- EOF > shellhub-agent.default
	SERVER_ADDRESS="${SHELLHUB_SERVER_ADDRESS}"
	PRIVATE_KEY="${SHELLHUB_PRIVATE_KEY}"
	EOF
}

do_install () {
    # Install the global configuration
    install -Dm 0644 shellhub-agent.default ${D}${sysconfdir}/default/shellhub-agent
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
