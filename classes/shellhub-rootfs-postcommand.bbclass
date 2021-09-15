# Allow overriding of ShellHub Tenant ID
#
# Required variable:
#
#  SHELLHUB_ROOTFS_TENANT_ID
#
#  The SHELLHUB_ROOTFS_TENANT_ID allow for override of existing ShellHub Tenant
#  ID during rootfs generation.
#
# Copyright 2021 (C) O.S. Systems Software LTDA.


# Override the TENANT_ID from existing ShellHub configuration file.
ROOTFS_POSTPROCESS_COMMAND += 'shellhub_override_tenant_id ;'
shellhub_override_tenant_id () {
    if [ ! -e "${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent" ]; then
        bbfatal "'${sysconfdir}/default/shellhub-agent' doesn't exist."
    fi

    if [ -z "${SHELLHUB_ROOTFS_TENANT_ID}" ]; then
        bbfatal "SHELLHUB_ROOTFS_TENANT_ID variable is undefined."
    fi

    sed -i 's%^TENANT_ID=.*%TENANT_ID="${SHELLHUB_ROOTFS_TENANT_ID}"%g' \
        ${IMAGE_ROOTFS}${sysconfdir}/default/shellhub-agent
}
