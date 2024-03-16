{
  inputs = {
    nixpkgs.url = github:nixos/nixpkgs/nixos-23.11;
    flake-utils.url = github:numtide/flake-utils;
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShell = pkgs.mkShell {
          name = "aquascape-shell";
          buildInputs = with pkgs; [
            scala_3
            sbt
            althttpd
            scala-cli
            visualvm
         ];
        };
      }
    );
}
