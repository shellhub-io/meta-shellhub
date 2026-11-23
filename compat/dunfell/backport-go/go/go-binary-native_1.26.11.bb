# This recipe is for bootstrapping our go-cross from a prebuilt binary of Go from golang.org.

SUMMARY = "Go programming language compiler (upstream binary for bootstrap)"
HOMEPAGE = " http://golang.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7998cb338f82d15c0eff93b7004d272a"

PROVIDES = "go-native"

# Checksums available at https://go.dev/dl/
SRC_URI = "https://dl.google.com/go/go${PV}.${BUILD_GOOS}-${BUILD_GOARCH}.tar.gz;name=go_${BUILD_GOTUPLE}"
SRC_URI[go_linux_amd64.sha256sum] = "9849f4e04ee35303714e34fbc3ac8ab44ea3e448a95aa182f91e3b9b79c710ec"
SRC_URI[go_linux_arm64.sha256sum] = "6c2f29b458b5add0835f18b776f69c8881a6d88428f1484a6da55755e620757e"
SRC_URI[go_linux_ppc64le.sha256sum] = "d0e8ef5248235b37c8eeac5af51b6cf114db3640226ed82b44d4af0d6341a490"

UPSTREAM_CHECK_URI = "https://golang.org/dl/"
UPSTREAM_CHECK_REGEX = "go(?P<pver>\d+(\.\d+)+)\.linux"

CVE_PRODUCT = "golang:go"

S = "${WORKDIR}/go"

inherit goarch native

do_compile() {
    :
}

make_wrapper() {
	rm -f ${D}${bindir}/$1
	cat <<END >${D}${bindir}/$1
#!/bin/bash
here=\`dirname \$0\`
export GOROOT="${GOROOT:-\`readlink -f \$here/../lib/go\`}"
\$here/../lib/go/bin/$1 "\$@"
END
	chmod +x ${D}${bindir}/$1
}

do_install() {
    find ${S} -depth -type d -name testdata -exec rm -rf {} +

	install -d ${D}${bindir} ${D}${libdir}/go
	cp --preserve=mode,timestamps -R ${S}/ ${D}${libdir}/

	for f in ${S}/bin/*
	do
	  	make_wrapper `basename $f`
	done
}
