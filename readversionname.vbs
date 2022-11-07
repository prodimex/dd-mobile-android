DIM returnValue
returnValue = 99

Set objFS = CreateObject("Scripting.FileSystemObject")
Set objFile = objFS.OpenTextFile(".\app\build.gradle")
verInt = 0

Do Until objFile.AtEndOfStream
    strLine = objFile.ReadLine
    If InStr(strLine,"versionCode")> 0 Then
        verInt = CInt(Replace(strLine, "versionCode", ""))
        strLine = "        versionCode " + CStr(verInt)
    End If
Loop

WScript.Echo Mid(CStr(verInt), 1, 1) & "." & Mid(CStr(verInt), 2, 1) & "." & Mid(CStr(verInt), 3, 2)