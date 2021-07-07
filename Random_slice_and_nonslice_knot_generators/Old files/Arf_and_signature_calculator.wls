#!/usr/bin/env wolframscript

<< KnotTheory`


If[Length[$ScriptCommandLine] >= 3, 
  unformattedBraidWord = ToString[$ScriptCommandLine[[2]]];
  identifier=ToString[$ScriptCommandLine[[3]]], 
  Print["Error: Not enough arguments entered."]];

coeffPairs[pol_, x_Symbol] := {#, Coefficient[pol, x, #]} & /@ Exponent[pol, x, List];
  

scriptPath = ExpandFileName[First[$ScriptCommandLine]];
scriptName = Last[FileNameSplit[ scriptPath ]];

Print["Mathematica ",unformattedBraidWord]

tempFile=StringReplace[scriptPath,scriptName -> StringJoin["tempfiles/ArfSignature",ToString[identifier],".txt"]]
  
braidWord=ToExpression[StringSplit[StringTrim[unformattedBraidWord, ("[" | "]")], ","]]

index=Max[Abs[braidWord]]+1

alexanderPolynomial=Alexander[BR[index,braidWord]][t]

alexanderPolynomialList=Sort[coeffPairs[alexanderPolynomial,t]]

arf = If[MemberQ[{3, 5}, Mod[alexanderPolynomial /. t -> -1, 8]], 1, 0]

signature=KnotSignature[BR[index,braidWord]]

determinant=Abs[alexanderPolynomial/.t->-1]

Export[tempFile,StringJoin[ToString[arf],"\[NewLine]",ToString[signature],"\[NewLine]",ToString[determinant],"\[NewLine]",ToString[alexanderPolynomialList]]]