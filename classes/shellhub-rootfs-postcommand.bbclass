# nooelint: oelint.bbclass.underscores oelint.file.inlinesuppress_na  no EXPORT_FUNCTIONS here, so the dash is harmless
# Allow overriding of the ShellHub registration credential
#
# Required variable, exactly one of:
#
#  SHELLHUB_ROOTFS_INSTALL_KEY
#
#  Overrides the registration credential with a namespace install key. This is
#  the supported path for a fleet.
#
#  SHELLHUB_ROOTFS_TENANT_ID
#
#  Overrides the registration credential with a tenant ID. Deprecated, kept
#  only for existing integrations.
#
# Copyright 2021 (C) O.S. Systems Software LTDA.

# "undefined" is the unset sentinel for both, matching shellhub-agent-config.
SHELLHUB_ROOTFS_INSTALL_KEY ??= "undefined"
SHELLHUB_ROOTFS_TENANT_ID ??= "undefined"

# Override the registration credential in an existing ShellHub configuration file.
ROOTFS_POSTPROCESS_COMMAND += "shellhub_override_registration;"
shellhub_override_registration[doc] = "Override the ShellHub registration credential in the rootfs config from SHELLHUB_ROOTFS_INSTALL_KEY or SHELLHUB_ROOTFS_TENANT_ID."
shellhub_override_registration () {
    if [ ! -e "${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent" ]; then
        bbfatal "'${sysconfdir}/default/shellhub-agent' doesn't exist."
    fi

    # An explicitly emptied value counts as unset alongside the sentinel, so
    # clearing an inherited SHELLHUB_ROOTFS_TENANT_ID is a way to move to an
    # install key.
    install_key="${SHELLHUB_ROOTFS_INSTALL_KEY}"
    if [ "$install_key" = "undefined" ]; then
        install_key=""
    fi

    tenant_id="${SHELLHUB_ROOTFS_TENANT_ID}"
    if [ "$tenant_id" = "undefined" ]; then
        tenant_id=""
    fi

    if [ -n "$install_key" ] && [ -n "$tenant_id" ]; then
        bbfatal "SHELLHUB_ROOTFS_INSTALL_KEY and SHELLHUB_ROOTFS_TENANT_ID are mutually exclusive; set only one of them."
    fi

    if [ -n "$install_key" ]; then
        credential="INSTALL_KEY=\"$install_key\""
    elif [ -n "$tenant_id" ]; then
        credential="TENANT_ID=\"$tenant_id\""
    else
        bbfatal "Set SHELLHUB_ROOTFS_INSTALL_KEY to override the registration credential. SHELLHUB_ROOTFS_TENANT_ID is also accepted, but registering by tenant ID is deprecated."
    fi

    # The config carries exactly one credential line, so rewriting either one
    # covers swapping a tenant-built image over to an install key.
    if ! grep -qE '^(INSTALL_KEY|TENANT_ID)=' ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent; then
        bbfatal "'${sysconfdir}/default/shellhub-agent' carries no registration credential to override."
    fi

    # Rewritten with awk rather than sed so the credential is substituted
    # literally; a '&' or a '%' in it would otherwise be taken as sed syntax.
    credential="$credential" awk '
        /^(INSTALL_KEY|TENANT_ID)=/ { print ENVIRON["credential"]; next }
        { print }
    ' ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent > ${WORKDIR}/shellhub-agent.override
    install -m 0644 ${WORKDIR}/shellhub-agent.override \
        ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent
    rm -f ${WORKDIR}/shellhub-agent.override
}
