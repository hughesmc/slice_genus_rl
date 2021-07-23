#!/usr/bin/env wolframscript

Print["YES"]

<< KnotTheory`


If[Length[$ScriptCommandLine] >= 3, 
  unformattedBraidWord = ToString[$ScriptCommandLine[[2]]];
  identifier=ToString[$ScriptCommandLine[[3]]], 
  Print["Error: Not enough arguments entered."]];

coeffPairs[pol_, x_Symbol] := {#, Coefficient[pol, x, #]} & /@ Exponent[pol, x, List];

coeffTriples[pol_, x_Symbol, y_Symbol] := 
 Module[{exponents1, exponents2, exponentPairs, coefficientTriples},
  exponents1 = Exponent[pol, x, List];
  exponents2 = Exponent[pol, y, List];
  exponentPairs = Tuples[{exponents1, exponents2}];
  coefficientTriples = 
   Table[{exponentPairs[[jjj, 1]], exponentPairs[[jjj, 2]], 
     Coefficient[Coefficient[pol, y, exponentPairs[[jjj, 2]]], x, 
      exponentPairs[[jjj, 1]]]},
    {jjj, 1, Length[exponentPairs]}];
  coefficientTriples = 
   Sort[DeleteCases[coefficientTriples, {_, _, 0}]];
  coefficientTriples];  
  

scriptPath = ExpandFileName[First[$ScriptCommandLine]];
scriptName = Last[FileNameSplit[ scriptPath ]];


Print["Mathematica ",unformattedBraidWord]

tempFile=StringReplace[scriptPath,scriptName -> StringJoin["tempfiles/invariants",ToString[identifier],".txt"]]

originalLocation=Directory[]

temLocation=StringJoin[originalLocation,"/mathematica_support/",ToString[identifier],"/"]
  
braidWord=ToExpression[StringSplit[StringTrim[unformattedBraidWord, ("[" | "]")], ","]]

index=Max[Abs[braidWord]]+1

alexanderPolynomial=Alexander[BR[index,braidWord]][t]

alexanderPolynomialList=Sort[coeffPairs[alexanderPolynomial,t]]

arf=If[MemberQ[{3, 5}, Mod[alexanderPolynomial /. t -> -1, 8]], 1, 0]

signature=KnotSignature[BR[index,braidWord]]

determinant=Abs[alexanderPolynomial/.t->-1]

SetDirectory[tempLocation]

khovanovPolynomial = Kh[BR[index,braidWord]][q, t]; 
   
khovanovPolynomialList = Sort[coeffTriples[khovanovPolynomial, q, t]]; 
   
jonesPolynomialList = Sort[coeffPairs[Simplify[Factor[khovanovPolynomial /. t -> -1]/(q + q^(-1))] /.q -> q^(1/2), q], #1[[1]] < #2[[1]] &];

jonesPolynomialString = StringReplace[ToString[jonesPolynomial], {"{" -> "[", "}" -> "]"}];

khovanovPolynomialString = StringReplace[ToString[khovanovPolynomial], {"{" -> "[", "}" -> "]"}];

SetDirectory[originalLocation]

Export[tempFile,StringJoin[ToString[arf],"\[NewLine]",ToString[signature],"\[NewLine]",ToString[determinant],"\[NewLine]",ToString[alexanderPolynomialList],"\[NewLine]",ToString[jonesPolynomialList],"\[NewLine]",ToString[khovanovPolynomialList]]]