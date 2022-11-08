<?php
	$apklist = scandir('.', 1);

	$buildVersion = $_GET['ver'];
	for($i=count($apklist)-1; $i>=0; $i--) {
		if(!strrpos($apklist[$i], $buildVersion)
			|| $apklist[$i]=='..'
			|| $apklist[$i]=='.'
		) {
			unset($apklist[$i]);
		}
	}
	if(count($apklist) == 0) {
		echo json_encode([
			'error' => 'updates not available'
		], JSON_UNESCAPED_UNICODE);
		return;
	}
	$apklist = array_values($apklist);
	$explode = explode('.apk', $apklist[0]);

	$versionName = $explode[0];
	$hostUrl = 'https://ddu.prodimex.ru/app-updates/driver/';
	$file = ''.$versionName.'.apk';
	$apkSize = filesize($file);
	$versionCode = preg_replace('~\D+~','', $versionName);
	echo json_encode([
		'title' => 'Обновление от 08.11.2022',
		'versionName' => $versionName,
		'versionCode' => (int)$versionCode,
		'apkUrl' => $hostUrl.$file,
		'description' => 'Пожалуйста загрузите это обновление и установите его, что бы иметь доступ ко всем новым функциям.',
		'grain' => rand(0, 100000),
		'release_changes' => [
            ['title' => '- Повышена мощность передатчика.'],
            ['title' => '- Исправлена путаница и "замораживание" маяков при передачи сигналов от погрузчика.'],
            ['title' => '- Исправлена ошибка при передаче сигналов на номера АМ с кодами регионов длинной больше 2х символов.'],
            ['title' => '- Данная версия предназначена для тестирования.']
		],
		'apkSize' => $apkSize,
		'apklist' => $apklist,
		'forceUpdate' => true
	], JSON_UNESCAPED_UNICODE);
?>
