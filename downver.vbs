Set objFS = CreateObject("Scripting.FileSystemObject")
strFile = ".\app\build.gradle"
Set objFile = objFS.OpenTextFile(".\app\build.gradle")
verInt = 0
Do Until objFile.AtEndOfStream
    strLine = objFile.ReadLine
    If InStr(strLine,"versionCode")> 0 Then
        ver = Replace(strLine, "versionCode", "")
        verInt = CInt(ver) - 1
        strLine = "        versionCode " + CStr(verInt)
    End If
    If InStr(strLine,"versionName")> 0 Then
        strLine = "        versionName " & CHR(34) & Mid(CStr(verInt), 1, 1) & "." & Mid(CStr(verInt), 2, 1) & "." & Mid(CStr(verInt), 3, 2) & CHR(34)
    End If
    WScript.Echo strLine
Loop