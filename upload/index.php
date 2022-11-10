<?php
	$apklist = scandir('.', 1);

	$buildMode = $_GET['build_mode'];
	$appMode = $_GET['app_mode'];
	for($i=count($apklist)-1; $i>=0; $i--) {
		if(!strrpos($apklist[$i], $appMode.'-'.$buildMode)
			|| $apklist[$i]=='..'
			|| $apklist[$i]=='.'
		) {
			unset($apklist[$i]);
		}
	}
	if(count($apklist) == 0) {
		echo json_encode([
			'error' => 'updates not available '.$appMode.'-'.$buildMode
		], JSON_UNESCAPED_UNICODE);
		return;
	}
	$apklist = array_values($apklist);
	$explode = explode('.apk', $apklist[0]);

	$versionName = $explode[0];
	$hostUrl = 'https://ddu.prodimex.ru/app-updates/';
	$file = ''.$versionName.'.apk';
	$apkSize = filesize($file);
	$versionCode = preg_replace('~\D+~','', $versionName);
	echo json_encode([
		'title' => 'Обновление от 09.11.2022 ('.$versionCode.')',
		'versionName' => $versionName,
		'versionCode' => (int)$versionCode,
		'apkUrl' => $hostUrl.$file,
		'description' => 'Пожалуйста загрузите это обновление и установите его, что бы иметь доступ ко всем новым функциям.',
		'grain' => rand(0, 100000),
		'release_changes' => [
		    ['title' => '- Улучшен процесс автодеплоя новых версий.'],
		    ['title' => '- Обработаны исключения приводящие к вылетам приложения.'],
            ['title' => '- Повышена мощность передатчика.'],
            ['title' => '- Внимание: данная версия предназначена для тестирования.']
		],
		'apkSize' => $apkSize,
		'apklist' => $apklist,
		'forceUpdate' => true
	], JSON_UNESCAPED_UNICODE);
?>
