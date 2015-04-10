#!/bin/bash
awk '{ if (NF!=6) {print} }' output/runs-$1.csv
