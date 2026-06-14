# nooelint: oelint.var.mandatoryvar.SRC_URI oelint.var.suggestedvar.CVE_PRODUCT  config recipe: it generates its config inline (no SRC_URI) and ships no code to CVE-track
SUMMARY = "ShellHub Configuration"
DESCRIPTION = "Generates the global ShellHub agent configuration (server address, private key path and tenant ID)."
HOMEPAGE = "https://shellhub.io"
BUGTRACKER = "https://github.com/shellhub-io/shellhub/issues"
SECTION = "console/network"
LICENSE = "MIT"
# nooelint: oelint.var.licenseremotefile  layer-internal recipe, no upstream source to carry a license file
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
INHIBIT_DEFAULT_DEPS = "1"

do_patch[noexec] = "1"
do_configure[noexec] = "1"

SHELLHUB_SERVER_ADDRESS ??= "https://cloud.shellhub.io"
SHELLHUB_PRIVATE_KEY ??= "${sysconfdir}/shellhub-agent.key"
SHELLHUB_TENANT_ID ??= "undefined"

# nooelint: oelint.task.noanonpython  required to SkipRecipe at parse time
python () {
    if d.getVar("SHELLHUB_TENANT_ID", False) == "undefined":
        raise bb.parse.SkipRecipe("To enable ShellHub support, the 'SHELLHUB_TENANT_ID' variable must be set.")
}

do_compile () {
    {
        echo "SERVER_ADDRESS=\"${SHELLHUB_SERVER_ADDRESS}\""
        echo "PRIVATE_KEY=\"${SHELLHUB_PRIVATE_KEY}\""
        echo "TENANT_ID=\"${SHELLHUB_TENANT_ID}\""
    } > shellhub-agent.default
}

do_install () {
    # Install the global configuration
    install -Dm 0644 shellhub-agent.default ${D}${sysconfdir}/default/shellhub-agent
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
