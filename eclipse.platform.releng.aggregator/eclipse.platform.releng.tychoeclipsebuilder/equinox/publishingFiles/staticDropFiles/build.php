<?php

function generateDropSize($zipfile) {
  $filesize = getDropSize($zipfile);
  return "<td>$filesize</td>";
}

function getDropSize($zipfile) {
  $filesize = "N/A";
  $filesizebytes  = filesize($zipfile);
  if($filesizebytes > 0) {
    if($filesizebytes < 1048576)
      $filesize = round($filesizebytes / 1024, 0) . "K";
    else if ($filesizebytes >= 1048576 && $filesizebytes < 10485760)
      $filesize = round($filesizebytes / 1048576, 1) . "M";
    else
      $filesize = round($filesizebytes / 1048576, 0) . "M";
  }
  return($filesize);
}

// TODO: change build index tool not to generate these "calls" at all. 
// In the mean time, we will just return empty string.
function generateChecksumLinks($zipfile, $buildDir) {
  //return "<td><a href=\"https://download.eclipse.org/equinox/drops/$buildDir/checksum/$zipfile.md5\"><img src=\"/equinox/images/md5.png\" alt=\"md5\"/></a><a href=\"https://download.eclipse.org/equinox/drops/$buildDir/checksum/$zipfile.sha1\"><img src=\"/equinox/images/sha1.png\" alt=\"sha1\"/></a></td>";
  //return "<td><a href=\"https://download.eclipse.org/equinox/drops/$buildDir/checksum/$zipfile.sha512\"><img src=\"/equinox/images/sha1.png\" alt=\"sha512\"/></a></td>";
  return "";
}
?>
