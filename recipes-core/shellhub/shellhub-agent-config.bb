SUMMARY = "ShellHub Configuration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
INHIBIT_DEFAULT_DEPS = "1"

do_patch[noexec] = "1"
do_configure[noexec] = "1"

SHELLHUB_SERVER_ADDRESS ??= "https://cloud.shellhub.io"
SHELLHUB_PRIVATE_KEY    ??= "${sysconfdir}/shellhub-agent.key"

# Registration credential. Set exactly one of the two.
#
# SHELLHUB_PROVISIONING_KEY registers the device through a namespace
# provisioning key, which applies the key's tags and acceptance mode to the
# whole batch. This is the supported path for a fleet.
#
# SHELLHUB_TENANT_ID registers through the namespace's legacy source, which
# seeds devices as pending for manual acceptance. It is deprecated and kept
# only for existing integrations.
SHELLHUB_PROVISIONING_KEY ??= "undefined"
SHELLHUB_TENANT_ID      ??= "undefined"

python () {
    # "undefined" is the unset sentinel for both credentials. An explicitly
    # emptied value counts as unset too, so clearing an inherited
    # SHELLHUB_TENANT_ID is a way to move to a provisioning key.
    provisioning_key = d.getVar("SHELLHUB_PROVISIONING_KEY")
    tenant_id = d.getVar("SHELLHUB_TENANT_ID")

    if provisioning_key in ("", "undefined"):
        provisioning_key = ""

    if tenant_id in ("", "undefined"):
        tenant_id = ""

    if provisioning_key and tenant_id:
        bb.fatal("'SHELLHUB_PROVISIONING_KEY' and 'SHELLHUB_TENANT_ID' are mutually exclusive; set only one of them.")

    if not provisioning_key and not tenant_id:
        raise bb.parse.SkipRecipe("To enable ShellHub support, set 'SHELLHUB_PROVISIONING_KEY' to a namespace provisioning key. "
                                  "'SHELLHUB_TENANT_ID' is also accepted, but registering by tenant ID is deprecated.")

    # Store the resolved values back so do_compile only has to test for
    # emptiness rather than repeat the sentinel handling in shell.
    d.setVar("SHELLHUB_PROVISIONING_KEY", provisioning_key)
    d.setVar("SHELLHUB_TENANT_ID", tenant_id)
}

do_compile () {
    {
        echo "SERVER_ADDRESS=\"${SHELLHUB_SERVER_ADDRESS}\""
        echo "PRIVATE_KEY=\"${SHELLHUB_PRIVATE_KEY}\""
        if [ -n "${SHELLHUB_PROVISIONING_KEY}" ]; then
            echo "PROVISIONING_KEY=\"${SHELLHUB_PROVISIONING_KEY}\""
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
