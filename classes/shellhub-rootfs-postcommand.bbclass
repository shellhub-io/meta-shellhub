# Allow overriding of the ShellHub registration credential
#
# Required variable, exactly one of:
#
#  SHELLHUB_ROOTFS_PROVISIONING_KEY
#
#  Overrides the registration credential with a namespace provisioning key.
#  This is the supported path for a fleet.
#
#  SHELLHUB_ROOTFS_TENANT_ID
#
#  Overrides the registration credential with a tenant ID. Deprecated, kept
#  only for existing integrations.
#
# Copyright 2021 (C) O.S. Systems Software LTDA.

# "undefined" is the unset sentinel for both, matching shellhub-agent-config.
SHELLHUB_ROOTFS_PROVISIONING_KEY ??= "undefined"
SHELLHUB_ROOTFS_TENANT_ID ??= "undefined"

# Override the registration credential in an existing ShellHub configuration file.
ROOTFS_POSTPROCESS_COMMAND += "shellhub_override_registration;"
shellhub_override_registration[doc] = "Override the ShellHub registration credential in the rootfs config from SHELLHUB_ROOTFS_PROVISIONING_KEY or SHELLHUB_ROOTFS_TENANT_ID."
shellhub_override_registration () {
    if [ ! -e "${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent" ]; then
        bbfatal "'${sysconfdir}/default/shellhub-agent' doesn't exist."
    fi

    # An explicitly emptied value counts as unset alongside the sentinel, so
    # clearing an inherited SHELLHUB_ROOTFS_TENANT_ID is a way to move to a
    # provisioning key.
    provisioning_key="${SHELLHUB_ROOTFS_PROVISIONING_KEY}"
    if [ "$provisioning_key" = "undefined" ]; then
        provisioning_key=""
    fi

    tenant_id="${SHELLHUB_ROOTFS_TENANT_ID}"
    if [ "$tenant_id" = "undefined" ]; then
        tenant_id=""
    fi

    if [ -n "$provisioning_key" ] && [ -n "$tenant_id" ]; then
        bbfatal "SHELLHUB_ROOTFS_PROVISIONING_KEY and SHELLHUB_ROOTFS_TENANT_ID are mutually exclusive; set only one of them."
    fi

    if [ -n "$provisioning_key" ]; then
        credential="PROVISIONING_KEY=\"$provisioning_key\""
    elif [ -n "$tenant_id" ]; then
        credential="TENANT_ID=\"$tenant_id\""
    else
        bbfatal "Set SHELLHUB_ROOTFS_PROVISIONING_KEY to override the registration credential. SHELLHUB_ROOTFS_TENANT_ID is also accepted, but registering by tenant ID is deprecated."
    fi

    # The config carries exactly one credential line, so rewriting either one
    # covers swapping a tenant-built image over to a provisioning key.
    if ! grep -qE '^(PROVISIONING_KEY|TENANT_ID)=' ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent; then
        bbfatal "'${sysconfdir}/default/shellhub-agent' carries no registration credential to override."
    fi

    # Rewritten with awk rather than sed so the credential is substituted
    # literally; a '&' or a '%' in it would otherwise be taken as sed syntax.
    credential="$credential" awk '
        /^(PROVISIONING_KEY|TENANT_ID)=/ { print ENVIRON["credential"]; next }
        { print }
    ' ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent > ${WORKDIR}/shellhub-agent.override
    install -m 0644 ${WORKDIR}/shellhub-agent.override \
        ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent
    rm -f ${WORKDIR}/shellhub-agent.override
}
