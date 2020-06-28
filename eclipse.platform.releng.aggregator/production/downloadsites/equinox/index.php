<?php
require_once($_SERVER['DOCUMENT_ROOT'] . "/eclipse.org-common/system/app.class.php");
require_once($_SERVER['DOCUMENT_ROOT'] . "/eclipse.org-common/system/nav.class.php");
require_once($_SERVER['DOCUMENT_ROOT'] . "/eclipse.org-common/system/menu.class.php");

$App 	= new App();
$Nav	= new Nav();
$Menu 	= new Menu();
include($App->getProjectCommon());

# Begin: page-specific settings.  Change these.
$pageTitle = "Equinox Downloads";
$pageKeywords = "equinox, osgi, framework, runtime, download";
$pageAuthor = "Equinox committers";

include('dlconfig.php');
#echo ("DOCUMENT_ROOT: " . $_SERVER['DOCUMENT_ROOT']."<br/>");

$aDirectory = dir("drops");
while ($anEntry = $aDirectory->read()) {

  // Short cut because we know aDirectory only contains other directories.

  if ($anEntry != "." && $anEntry!=".." && $anEntry!="TIME") {
    // do not count hidden directories in computation
    // allows non-hidden ones to still show up as "most recent" else will be blank.
    if (!file_exists("drops/".$anEntry."/buildHidden")) {
      $parts = explode("-", $anEntry);
      if (count($parts) == 3) {

        //$buckets[$parts[0]][] = $anEntry;

        $timePart = $parts[2];
        $year = substr($timePart, 0, 4);
        $month = substr($timePart, 4, 2);
        $day = substr($timePart, 6, 2);
        $hour = substr($timePart,8,2);
        $minute = substr($timePart,10,2);
        // special logic adds 1 second if build id contains "RC" ... this was
        // added for the M build case, where there is an M build and and RC version that
        // have same time stamp. One second should not effect displayed values.
        $isRC = strpos($anEntry, "RC");
        if ($isRC === false) {
          $timeStamp = mktime($hour, $minute, 0, $month, $day, $year);
        } else {
          $timeStamp = mktime($hour, $minute, 1, $month, $day, $year);
        }
        $buckets[$parts[0]][$timeStamp] = $anEntry;
        $timeStamps[$anEntry] = date("D, j M Y -- H:i (O)", $timeStamp);
        if (isset($latestTimeStamp) && array_key_exists($parts[0], $latestTimeStamp)) {
          if ($timeStamp > $latestTimeStamp[$parts[0]]) {
            $latestTimeStamp[$parts[0]] = $timeStamp;
            $latestFile[$parts[0]] = $anEntry;
          }
        } else {
          $latestTimeStamp[$parts[0]] = $timeStamp;
          $latestFile[$parts[0]] = $anEntry;
        }

      }

      if (count($parts) == 2) {
        $buildType=substr($parts[0],0,1);
        //$buckets[$buildType][] = $anEntry;
        $datePart = substr($parts[0],1);
        $timePart = $parts[1];
        $year = substr($datePart, 0, 4);
        $month = substr($datePart, 4, 2);
        $day = substr($datePart, 6, 2);
        $hour = substr($timePart,0,2);
        $minute = substr($timePart,2,2);
        $isRC = strpos($anEntry, "RC");
        if ($isRC === false) {
          $timeStamp = mktime($hour, $minute, 0, $month, $day, $year);
        } else {
          $timeStamp = mktime($hour, $minute, 1, $month, $day, $year);
        }
        $buckets[$buildType[0]][$timeStamp] = $anEntry;

        $timeStamps[$anEntry] = date("D, j M Y -- H:i (O)", $timeStamp);
        if (isset($latestTimeStamp) && array_key_exists($buildType,$latestTimeStamp)) {
          if ($timeStamp > $latestTimeStamp[$buildType]) {
            $latestTimeStamp[$buildType] = $timeStamp;
            $latestFile[$buildType] = $anEntry;
          }
        } else {
          $latestTimeStamp[$buildType] = $timeStamp;
          $latestFile[$buildType] = $anEntry;
        }
      }
    }
  }
}

$html = <<<EOHTML
<div id="midcolumn">
        <h3>$pageTitle</h3>
        <p>Older releases are available in the <a href="https://archive.eclipse.org/equinox/">Equinox archived builds site</a>.</p>

        <div class="homeitem3col">
                <h3>Latest Builds</h3>
                <table  width="100%" CELLSPACING=0 CELLPADDING=3>

EOHTML;

foreach($dropType as $value) {
  $prefix=$typeToPrefix[$value];
  if (array_key_exists($prefix, $latestFile)) {
    $fileName = $latestFile[$prefix];
    $parts = explode("-", $fileName);

    // Uncomment the line below if we need click through licenses.
    // echo "<td><a href=license.php?license=drops/$fileName>$parts[1]</a></td>";

    // Comment the line below if we need click through licenses.
    if (count($parts)==3)
      $html .= <<<EOHTML
                        <tr>
                                <td width="30%"><a href="drops/$fileName/index.php">$parts[1]</a></td>

EOHTML;
    if (count($parts)==2)
      $html .= <<<EOHTML
                        <tr>
                                <td width="30%"><a href="drops/$fileName/index.php">$fileName</a></td>

EOHTML;

    $html .= <<<EOHTML
                                <td>$value</td>
                                <td>$timeStamps[$fileName]</td>
                        </tr>

EOHTML;
  }
}
$html .= <<<EOHTML
                </table>

EOHTML;

foreach($dropType as $value) {
  $prefix=$typeToPrefix[$value];

  $html .= <<<EOHTML

                <h3>$value</h3>
                <table  width="100%" CELLSPACING=0 CELLPADDING=3>

EOHTML;
  if (array_key_exists($prefix,$buckets)) {
    $aBucket = $buckets[$prefix];
    if (isset($aBucket)) {
      krsort($aBucket);
      foreach($aBucket as $innerValue) {
        $parts = explode("-", $innerValue);
        $html .= <<<EOHTML
                        <tr>

EOHTML;
        // Uncomment the line below if we need click through licenses.
        // echo "<td><a href=\"license.php?license=drops/$innerValue\">$parts[1]</a></td>";

        // Comment the line below if we need click through licenses.
        if (count ($parts)==3)
          $html .= <<<EOHTML
                                <td width="30%"><a href="drops/$innerValue/index.php">$parts[1]</a></td>

EOHTML;
        if (count ($parts)==2)
          $html .= <<<EOHTML
                                <td width="30%"><a href="drops/$innerValue/index.php">$innerValue</a></td>

EOHTML;

        $html .= <<<EOHTML
                                <td>$timeStamps[$innerValue]</td>
                        </tr>

EOHTML;
      }
    }
  }
  $html .= <<<EOHTML
                </table>

EOHTML;
}
$html .= <<<EOHTML
        </div>
</div>

EOHTML;

generateRapPage( $App, $Menu, $Nav, $pageAuthor, $pageKeywords, $pageTitle, $html );
?>
