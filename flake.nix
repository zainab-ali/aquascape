{
  inputs = {
    nixpkgs.url = github:nixos/nixpkgs/nixos-23.11;
    flake-utils.url = github:numtide/flake-utils;
    typelevel-nix.url = "github:typelevel/typelevel-nix";
    typelevel-nix.inputs.nixpkgs.follows = "nixpkgs";
    typelevel-nix.inputs.flake-utils.follows = "flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlay ];
        };
      in
      {
        devShell = pkgs.devshell.mkShell {
          packages = [pkgs.althttpd];
          imports = [ typelevel-nix.typelevelShell ];
          name = "aquascape-shell";
          typelevelShell = {
            jdk.package = pkgs.jdk21;
            native.enable = false;
            nodejs.enable = true;
          };
        };
      }
    );
}
