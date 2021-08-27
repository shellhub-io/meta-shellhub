SUMMARY = "ShellHub runtime support"

LICENSE = "MIT"

inherit packagegroup

PACKAGES += "${PN}-runtime"

RDEPENDS:${PN}-runtime += " \
    shellhub-agent \
"
