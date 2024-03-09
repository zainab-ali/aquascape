{
  inputs = {
    nixpkgs.url = github:nixos/nixpkgs/nixos-23.05;
    flake-utils.url = github:numtide/flake-utils;
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShell = pkgs.mkShell {
          name = "stoop-shell";
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
