#!/usr/bin/env wolframscript

<< KnotTheory`


If[Length[$ScriptCommandLine] >= 3, 
  unformattedBraidWord = ToString[$ScriptCommandLine[[2]]];
  identifier=ToString[$ScriptCommandLine[[3]]], 
  Print["Error: Not enough arguments entered."]];
  

scriptPath = ExpandFileName[First[$ScriptCommandLine]];
scriptName = Last[FileNameSplit[ scriptPath ]];

tempFile=StringReplace[scriptPath,scriptName -> StringJoin["tempfiles/nonsliceArfSignature",ToString[identifier],".txt"]]
  
braidWord=ToExpression[StringSplit[StringTrim[unformattedBraidWord, ("[" | "]")], ","]]

index=Max[Abs[braidWord]]+1

alexanderPolynomial=Alexander[BR[index,braidWord]][t]

arf = If[MemberQ[{3, 5}, Mod[alexanderPolynomial /. t -> -1, 8]], 1, 0]

signature=KnotSignature[BR[index,braidWord]]

Export[tempFile,StringJoin[ToString[arf],"\[NewLine]",ToString[signature]]]