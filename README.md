# OpenEmbedded/Yocto Project support layer for ShellHub

ShellHub enables teams to easily access any Linux devices, behind
firewall or NAT.

## Dependencies

This layer depends on:

```shell
  URI: git://git.openembedded.org/bitbake
  branch: master

  URI: git://git.openembedded.org/openembedded-core
  layers: meta
  branch: master
```

## Notes for using with Warrior release

ShellHub uses Go Lang version 1.14.x and the related recipes and classes were
backported from the OpenEmbedded-Core layer. The `meta-shellhub` layer has a higher
priority than `openembedded-core` or` poky`, you need to add` meta-shellhub`
before core layer in `bblayers.conf` to build `shellhub-agent` correctly.

## Contributing

ShellHub is an open source project and we love to receive
contributions from our community.  If you would like to contribute,
please read our [contributing guide](https://github.com/ShellHub-io/shellhub/blob/master/CONTRIBUTING.md).

## License

UpdateHub Yocto Project Reference Platform is licensed under the MIT License.
See [COPYING.MIT](COPYING.MIT) for the full license text.

## Getting in touch

* All source code are in [Github](https://github.com/ShellHub-io)
