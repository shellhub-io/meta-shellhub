SUMMARY = "ShellHub runtime support"

LICENSE = "MIT"

inherit packagegroup

PACKAGES += "${PN}-runtime"

RDEPENDS_${PN}-runtime += " \
    shellhub-agent \
"
