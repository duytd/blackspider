package com.portia

import com.portia.algorithms.NaiveBayesClassifier

/**
 * Classifier main program
 * @author duytd
 */
object ClassifierMain {
  def main(args:Array[String]): Unit ={
    val nbc = new NaiveBayesClassifier
    val testUrls = Array(
      "http://dantri.com.vn/su-kien/hlv-miura-cong-bo-18-cau-thu-tap-trung-doi-tuyen-quoc-gia-1065884.htm",
    "http://thethao.vnexpress.net/tin-tuc/bong-da-trong-nuoc/cong-vinh-bi-tuoc-ban-thang-nhanh-nhat-lich-su-v-league-3208958.html",
    "http://thethao.vnexpress.net/tin-tuc/la-liga/barca-chay-da-cho-dai-chien-voi-bayern-bang-tran-thang-8-0-3208231.html",
    "http://thethao.vnexpress.net/tin-tuc/tennis/federer-chung-nhanh-ban-ket-voi-nadal-o-madrid-masters-2015-3208441.html",
    "http://thethao.vnexpress.net/tin-tuc/tennis/murray-thang-tran-dau-tien-tu-sau-dam-cuoi-3207873.html",
    "http://thethao.vnexpress.net/tin-tuc/tennis/federer-cham-moc-200-chien-thang-tren-san-dat-nen-3207703.html",
    "http://thethao.vnexpress.net/tin-tuc/tennis/ly-hoang-nam-thang-tien-vao-tu-ket-giai-thai-lan-3207598.html",
    "http://thethao.vnexpress.net/tin-tuc/giai-ngoai-hang-anh/lampard-gui-loi-chuc-mung-chelsea-vo-dich-3208620.html",
    "http://thethao.vnexpress.net/tin-tuc/giai-ngoai-hang-anh/van-persie-hong-phat-den-man-utd-thua-tran-thu-ba-lien-tiep-3208247.html",
    "http://thethao.vnexpress.net/tin-tuc/giai-ngoai-hang-anh/wenger-lai-boi-nho-mourinho-3208012.html"
    )
    testUrls.foreach(url=>{
      val category = nbc.classifyPageByUrl(url)
      println(url+" belongs to "+category.name)
    })
  }
}
