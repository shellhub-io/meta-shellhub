# nooelint: oelint.var.mandatoryvar.SRC_URI oelint.var.suggestedvar.CVE_PRODUCT  config recipe: it generates its config inline (no SRC_URI) and ships no code to CVE-track
SUMMARY = "ShellHub Configuration"
DESCRIPTION = "Generates the global ShellHub agent configuration (server address, private key path and the registration credential, either an install key or a tenant ID)."
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

# Registration credential. Set exactly one of the two.
#
# SHELLHUB_INSTALL_KEY registers the device through a namespace install key,
# which applies the key's tags and acceptance mode to the whole batch. This is
# the supported path for a fleet.
#
# SHELLHUB_TENANT_ID registers through the namespace's legacy source, which
# seeds devices as pending for manual acceptance. It is deprecated and kept
# only for existing integrations.
SHELLHUB_INSTALL_KEY ??= "undefined"
SHELLHUB_TENANT_ID ??= "undefined"

# nooelint: oelint.task.noanonpython  required to SkipRecipe at parse time
python () {
    # "undefined" is the unset sentinel for both credentials. An explicitly
    # emptied value counts as unset too, so clearing an inherited
    # SHELLHUB_TENANT_ID is a way to move to an install key.
    install_key = d.getVar("SHELLHUB_INSTALL_KEY")
    tenant_id = d.getVar("SHELLHUB_TENANT_ID")

    if install_key in ("", "undefined"):
        install_key = ""

    if tenant_id in ("", "undefined"):
        tenant_id = ""

    if install_key and tenant_id:
        bb.fatal("'SHELLHUB_INSTALL_KEY' and 'SHELLHUB_TENANT_ID' are mutually exclusive; set only one of them.")

    if not install_key and not tenant_id:
        raise bb.parse.SkipRecipe("To enable ShellHub support, set 'SHELLHUB_INSTALL_KEY' to a namespace install key. "
                                  "'SHELLHUB_TENANT_ID' is also accepted, but registering by tenant ID is deprecated.")

    # Store the resolved values back so do_compile only has to test for
    # emptiness rather than repeat the sentinel handling in shell.
    d.setVar("SHELLHUB_INSTALL_KEY", install_key)
    d.setVar("SHELLHUB_TENANT_ID", tenant_id)
}

do_compile () {
    {
        echo "SERVER_ADDRESS=\"${SHELLHUB_SERVER_ADDRESS}\""
        echo "PRIVATE_KEY=\"${SHELLHUB_PRIVATE_KEY}\""
        if [ -n "${SHELLHUB_INSTALL_KEY}" ]; then
            echo "INSTALL_KEY=\"${SHELLHUB_INSTALL_KEY}\""
        else
            echo "TENANT_ID=\"${SHELLHUB_TENANT_ID}\""
        fi
    } > shellhub-agent.default
}

do_install () {
    # Install the global configuration
    install -Dm 0644 shellhub-agent.default ${D}${sysconfdir}/default/shellhub-agent
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
