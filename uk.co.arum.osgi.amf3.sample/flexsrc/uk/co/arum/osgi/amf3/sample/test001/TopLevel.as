package uk.co.arum.osgi.amf3.sample.test001
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="uk.co.arum.osgi.amf3.sample.test001.TopLevel")]
	public class TopLevel
	{
		
		public var midLevels:ArrayCollection;
		
		public function TopLevel()
		{
		}

	}
}