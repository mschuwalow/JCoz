{ pkgs ? import <nixpkgs> { } }:
pkgs.mkShell { buildInputs = with pkgs; [ spdlog gcc openjdk8 gnumake maven ]; }
