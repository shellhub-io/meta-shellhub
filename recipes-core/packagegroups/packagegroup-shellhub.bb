SUMMARY = "ShellHub runtime support"
DESCRIPTION = "Packagegroup pulling in the ShellHub agent and its runtime dependencies."
BUGTRACKER = "https://github.com/shellhub-io/shellhub/issues"
SECTION = "console/network"

LICENSE = "MIT"

inherit packagegroup

PACKAGES += "${PN}-runtime"

RDEPENDS:${PN}-runtime = "shellhub-agent"
