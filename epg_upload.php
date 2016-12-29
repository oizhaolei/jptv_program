<?php
date_default_timezone_set("Asia/Shanghai");
//print_r($_FILES);

if ($_FILES) {
 $filename = $_FILES['file']['name'];
 $tmpname = $_FILES['file']['tmp_name'];
 $uploaddir = '/tmp/';  
 $uploadfile = $uploaddir . basename($filename);
 //echo '$tmpname='.$tmpname.'$filename='.$filename.'<br/>';
 $static_key = "a4b8c1x0y7z4";
 $now = date('Ymd');
 $hashid = md5($now. $static_key);
 //echo '$now=' . $now .', $static_key=' . $static_key . ', $hashid=' . $hashid.'<br/>';
 
 if ('tokyo_' . $now . '_' . $hashid . '.zip' != $filename
  && 'osaka_' . $now . '_' . $hashid . '.zip' != $filename) {
  unlink($tmpname);
  echo 'DELETE';
 } else {
 	//echo '$tmpname='.$tmpname.',$uploadfile='.$uploadfile;
  if(move_uploaded_file($tmpname, $uploadfile)) {
   echo 'SUCCEED';
  } else {
   echo 'FAIL';
  }
 }
} else {
  echo 'NO FILE';
}

?>